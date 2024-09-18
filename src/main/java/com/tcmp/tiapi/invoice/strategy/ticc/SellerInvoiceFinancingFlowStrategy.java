package com.tcmp.tiapi.invoice.strategy.ticc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.InvoiceRealOutputData;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.exception.InconsistentInvoiceInformationException;
import com.tcmp.tiapi.invoice.model.EventExtension;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import com.tcmp.tiapi.invoice.repository.EventExtensionRepository;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.repository.redis.BulkCreateInvoicesFileInfoRepository;
import com.tcmp.tiapi.invoice.service.files.realoutput.InvoiceRealOutputFileUploader;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.program.repository.ProgramExtensionRepository;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.ti.route.ticc.TICCIncomingStrategy;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadDetails;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadInvoice;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadStatus;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.CorporateLoanMapper;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.DistributorCreditRequest;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Amortization;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Data;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.corporateloan.exception.CreditCreationException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titofcm.dto.SinglePaymentMapper;
import com.tcmp.tiapi.titofcm.dto.request.SinglePaymentRequest;
import com.tcmp.tiapi.titofcm.dto.response.SinglePaymentResponse;
import com.tcmp.tiapi.titofcm.exception.SinglePaymentException;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import com.tcmp.tiapi.titofcm.repository.InvoicePaymentCorrelationInfoRepository;
import com.tcmp.tiapi.titofcm.service.SingleElectronicPaymentService;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SellerInvoiceFinancingFlowStrategy implements TICCIncomingStrategy {
    private final UUIDGenerator uuidGenerator;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EventExtensionRepository eventExtensionRepository;
    private final BulkCreateInvoicesFileInfoRepository createInvoicesFileInfoRepository;
    private final InvoiceRealOutputFileUploader realOutputFileUploader;
    private final InvoicePaymentCorrelationInfoRepository invoicePaymentCorrelationInfoRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductMasterExtensionRepository productMasterExtensionRepository;
    private final ProgramExtensionRepository programExtensionRepository;

    private final CorporateLoanMapper corporateLoanMapper;
    private final SinglePaymentMapper singlePaymentMapper;
    private final SingleElectronicPaymentService singleElectronicPaymentService;
    private final CorporateLoanService corporateLoanService;
    private final OperationalGatewayService operationalGatewayService;
    private final BusinessBankingService businessBankingService;
    /**
     * This function receives the financing result message from TI. First, it sends a notification to
     * the seller that the invoice has been financed, then it creates a credit and transfers the
     * disbursement amount from buyer to seller, the flows continues with a credit simulation (to know
     * how much taxes does the seller have to pay) and then a seller to buyer transaction is created,
     * then a notification is sent to the seller with a processed status and finally the result is
     * notified to business banking.
     *
     * @param serviceRequest The `TFSCFCRE` message.
     */
    @Override
    public void handleServiceRequest(AckServiceRequest<?> serviceRequest){
        FinanceAckMessage financeMessage = (FinanceAckMessage) serviceRequest.getBody();
        String masterReference = financeMessage.getInvoiceArray().get(0).getInvoiceReference();

        log.info(
                "Starting financing flow for seller invoice [{}] with master ref [{}].",
                financeMessage.getTheirRef(),
                masterReference
        );

        InvoiceMaster invoice = findInvoiceByMasterReference(masterReference);
        ProductMasterExtension invoiceExtension = findMasterExtensionByReference(masterReference);
        invoice.setProductMasterExtension(invoiceExtension);

        try{
            Customer buyer = findCustomerByMnemonic(financeMessage.getBuyerIdentifier());
            Customer seller = findCustomerByMnemonic(financeMessage.getSellerIdentifier());
            ProgramExtension programExtension = findByProgramIdOrDefault(financeMessage.getProgramme());

            EncodedAccountParser buyerAccountParser = new EncodedAccountParser(invoiceExtension.getFinanceAccount());
            EncodedAccountParser sellerAccountParser = findSelectedSellerAccountOrDefault(financeMessage);

            sendEmailToCustomer(InvoiceEmailEvent.FINANCED, financeMessage, seller);
            DistributorCreditResponse buyerCredit =
                    createBuyerCredit(financeMessage, programExtension, buyer, buyerAccountParser);
            saveGafOperationBuyerInformation(buyerCredit, invoiceExtension);
            SinglePaymentResponse response = transferCreditAmountFromBuyerToSeller(
                    financeMessage, buyer, seller, buyerAccountParser, sellerAccountParser);
            saveInitialPaymentCorrelationInfo(response.data().paymentReferenceNumber(), financeMessage);
        }catch(Exception e){
            handleFinancingError(e, financeMessage, invoice);
        }

    }

    private InvoiceMaster findInvoiceByMasterReference(String invoiceMasterReference){
    return invoiceRepository
        .findByProductMasterMasterReference(invoiceMasterReference)
        .orElseThrow(() -> new InconsistentInvoiceInformationException("Could not find invoice for the given invoice master."));
    }

    private ProductMasterExtension findMasterExtensionByReference(String invoiceMasterReference){
        return productMasterExtensionRepository.findByMasterReference(invoiceMasterReference)
                .orElseThrow( () -> new InconsistentInvoiceInformationException( "Could not find extension for master "+ invoiceMasterReference));
    }

    private Customer findCustomerByMnemonic(String customerMnemonic){
        return customerRepository.findFirstByIdMnemonic(customerMnemonic)
                .orElseThrow( () -> new InconsistentInvoiceInformationException( "Could not find customer with mnemonic " + customerMnemonic));
    }

    private ProgramExtension findByProgramIdOrDefault(String programmeId) {
        return programExtensionRepository
                .findByProgrammeId(programmeId)
                .orElse(
                        ProgramExtension.builder()
                                .programmeId(programmeId)
                                .extraFinancingDays(0)
                                .requiresExtraFinancing(false)
                                .build()
                );
    }

    private EncodedAccountParser findSelectedSellerAccountOrDefault( FinanceAckMessage financeMessage) {
        return eventExtensionRepository
                .findByMasterReference(financeMessage.getMasterRef())
                .map(EventExtension::getFinanceSellerAccount)
                .filter(account -> !account.isBlank())
                .map(EncodedAccountParser::new)
                .orElseGet( () -> {
                    String sellerMnemonic = financeMessage.getSellerIdentifier();
                    Account defaultSellerAccount = findAccountByCustomerMnemonic(sellerMnemonic);
                    return new EncodedAccountParser(defaultSellerAccount.getExternalAccountNumber());
                });
    }

    private Account findAccountByCustomerMnemonic(String customerMnemonic){
        return accountRepository
                .findByTypeAndCustomerMnemonic("CA", customerMnemonic)
                .orElseThrow(
                        () -> new InconsistentInvoiceInformationException(
                                "Could not find account for customer "+customerMnemonic
                        )
                );
    }

    private void sendEmailToCustomer(
            InvoiceEmailEvent event, FinanceAckMessage financeMessage, Customer seller){
        String invoiceNumber = financeMessage.getTheirRef().split("--")[0];

        BigDecimal financeDealAmount = getFinanceDealAmountFromMessage(financeMessage);

        InvoiceEmailInfo financedInvoiceInfo =
                InvoiceEmailInfo.builder()
                        .customerMnemonic(financeMessage.getSellerIdentifier())
                        .customerEmail(seller.getAddress().getCustomerEmail().trim())
                        .customerName(seller.getFullName().trim())
                        .date(financeMessage.getStartDate())
                        .action(event.getValue())
                        .invoiceCurrency(financeMessage.getFinanceDealCurrency())
                        .invoiceNumber(invoiceNumber)
                        .amount(financeDealAmount)
                        .build();
        operationalGatewayService.sendNotificationRequest(financedInvoiceInfo);
    }

    private BigDecimal getFinanceDealAmountFromMessage(FinanceAckMessage invoiceFinanceAck){
        BigDecimal financeDealAmountInCents = new BigDecimal(invoiceFinanceAck.getFinanceDealAmount());
        return MonetaryAmountUtils.convertCentsToDollars(financeDealAmountInCents);
    }

    private DistributorCreditResponse createBuyerCredit(
            FinanceAckMessage financeMessage, ProgramExtension programExtension, Customer buyer, EncodedAccountParser buyerAccountParser)
        throws CreditCreationException {
        log.info("Starting credit creation.");
        int term = calculateCreditTermForBuyer( financeMessage, programExtension);
        DistributorCreditRequest credit =
                corporateLoanMapper.mapToFinanceRequest(
                        financeMessage, programExtension, buyer, buyerAccountParser, term);
        DistributorCreditResponse buyerCredit = corporateLoanService.createCredit(credit);
        Error creditError = buyerCredit.data().error();

        boolean hasBeenCredited = creditError != null && creditError.hasNoError();
        if(!hasBeenCredited) {
            String creditErrorMessage =
                    creditError != null ? creditError.message() : "Credit creation failed.";
            throw new CreditCreationException(creditErrorMessage);
        }
        return buyerCredit;
    }

    private int calculateCreditTermForBuyer(
            FinanceAckMessage invoiceFinanceMessage, ProgramExtension programExtension){
        int extraFinancingDays = programExtension.getExtraFinancingDays();
        return extraFinancingDays + calculateCreditTermForSeller(invoiceFinanceMessage);
    }

    private int calculateCreditTermForSeller(FinanceAckMessage invoiceFinanceMessage){
        LocalDate startDate = LocalDate.parse(invoiceFinanceMessage.getStartDate());
        LocalDate maturityDate = LocalDate.parse(invoiceFinanceMessage.getMaturityDate());

        return (int) ChronoUnit.DAYS.between(startDate, maturityDate);
    }

    private void saveGafOperationBuyerInformation (
            DistributorCreditResponse creditResponse, ProductMasterExtension invoiceExtension){
        BigDecimal buyerGafInterests = getInterests(creditResponse);
        Data credit = creditResponse.data();

        invoiceExtension.setGafOperationId(credit.operationId());
        invoiceExtension.setGafInterestRate(BigDecimal.valueOf(credit.disbursementAmount()));
        invoiceExtension.setGafDisbursementAmount(BigDecimal.valueOf(credit.disbursementAmount()));
        invoiceExtension.setGafTaxFactor(BigDecimal.valueOf(credit.tax().factor()));
        invoiceExtension.setBuyerGafInterests(buyerGafInterests);
        invoiceExtension.setBuyerSolcaAmount(BigDecimal.valueOf(credit.tax().amount()));
        invoiceExtension.setAmortizations(getAmortizationsPayLoad(creditResponse));

        productMasterExtensionRepository.save(invoiceExtension);

        log.info("Saved GAF information for invoice extension with id [{}].", invoiceExtension.getId());
    }

    private BigDecimal getInterests(DistributorCreditResponse creditResponse){
        return creditResponse.data().amortizations().stream()
                .filter(a -> "IV".equals(a.code()))
                .findFirst()
                .map(Amortization::amount)
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);
    }

    private String getAmortizationsPayLoad(DistributorCreditResponse creditResponse){
        try {
            return objectMapper.writeValueAsString(creditResponse.data().amortizations());
        } catch (JsonProcessingException e){
            log.error(e.getMessage());
            return "";

        }
    }

    private SinglePaymentResponse transferCreditAmountFromBuyerToSeller(
            FinanceAckMessage financeMessage, Customer buyer, Customer seller, EncodedAccountParser buyerAccountParser, EncodedAccountParser sellerAccountParser){
        log.info("Stasting buyer to seller transaction.");
        String invoiceNumber = financeMessage.getTheirRef().split("--")[0];
        String debitDescription =
                String.format("Debito Fact %s %s", invoiceNumber, financeMessage.getSellerName());
        String creditDescription =
                String.format("Pago Fact %s %s", invoiceNumber, financeMessage.getBuyerIdentifier());

        SinglePaymentRequest buyerToSellerPayment =
                singlePaymentMapper.mapFinanceCustomerToCustomerTransaction(
                        financeMessage,
                        buyer,
                        seller,
                        buyerAccountParser,
                        sellerAccountParser,
                        debitDescription,
                        creditDescription,
                        getFinanceDealAmountFromMessage(financeMessage));
        return singleElectronicPaymentService.createSinglePayment(buyerToSellerPayment);
    }

    private void saveInitialPaymentCorrelationInfo(
            String paymentReference, FinanceAckMessage financeMessage){
        try{
            InvoicePaymentCorrelationInfo info =
                    InvoicePaymentCorrelationInfo.builder()
                            .id(uuidGenerator.getNewId())
                            .paymentReference(paymentReference)
                            .initialEvent(InvoicePaymentCorrelationInfo.InitialEvent.SELLER_CENTRIC_FINANCE_0)
                            .eventPayload(objectMapper.writeValueAsString(financeMessage))
                            .build();
            invoicePaymentCorrelationInfoRepository.save(info);
            log.info("Payment correlation info saved. Id={}", info.getId());
        } catch (JsonProcessingException e) {
            throw new InconsistentInvoiceInformationException(e.getMessage());
        }
    }

    private void handleFinancingError(
            Throwable e, FinanceAckMessage financeMessage, InvoiceMaster invoice) {
        log.error(e.getMessage());

        boolean isNotifiableError =
                e instanceof CreditCreationException
                        || e instanceof SinglePaymentException
                        || e instanceof InconsistentInvoiceInformationException
                        || e instanceof EncodedAccountParser.AccountDecodingException;
        if (!isNotifiableError) return;

        if (isCreatedViaSftp(invoice)) {
            notifyStatusViaSftp(InvoiceRealOutputData.Status.FAILED, financeMessage, invoice);
            notifyStatusViaBusinessBanking(
                    PayloadStatus.FAILED,
                    OperationalGatewayProcessCode.INVOICE_FINANCING_SFTP,
                    financeMessage,
                    invoice,
                    e.getMessage());
            return;
        }

        notifyStatusViaBusinessBanking(
                PayloadStatus.FAILED,
                OperationalGatewayProcessCode.INVOICE_FINANCING,
                financeMessage,
                invoice,
                e.getMessage());
    }

    private boolean isCreatedViaSftp(InvoiceMaster invoice) {
        String fileUuid = invoice.getProductMasterExtension().getFileCreationUuid();
        return fileUuid != null && !fileUuid.isBlank();
    }

    private void notifyStatusViaSftp(
            InvoiceRealOutputData.Status status,
            FinanceAckMessage financeMessage,
            InvoiceMaster invoice) {
        String fileUuid = invoice.getProductMasterExtension().getFileCreationUuid().trim();

        BulkCreateInvoicesFileInfo fileInfo =
                createInvoicesFileInfoRepository
                        .findById(fileUuid)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException("Could not find file info with uuid " + fileUuid));

        InvoiceRealOutputData realOutputRow =
                InvoiceRealOutputData.builder()
                        .invoiceReference(invoice.getReference().trim())
                        .processedAt(LocalDateTime.now(clock))
                        .status(status)
                        .amount(getFinanceDealAmountFromMessage(financeMessage))
                        .counterPartyMnemonic(financeMessage.getSellerIdentifier())
                        .build();
        realOutputFileUploader.appendInvoiceStatusRow(fileInfo.getOriginalFilename(), realOutputRow);
    }

    private void notifyStatusViaBusinessBanking(
            PayloadStatus status,
            OperationalGatewayProcessCode processCode,
            FinanceAckMessage financeResultMessage,
            InvoiceMaster invoice,
            @Nullable String error) {
        List<String> errors = error == null ? null : List.of(error);

        OperationalGatewayRequestPayload payload =
                OperationalGatewayRequestPayload.builder()
                        .status(status.getValue())
                        .invoice(
                                PayloadInvoice.builder()
                                        .batchId(invoice.getBatchId().trim())
                                        .reference(financeResultMessage.getTheirRef())
                                        .sellerMnemonic(financeResultMessage.getSellerIdentifier())
                                        .build())
                        .details(new PayloadDetails(errors, null, null))
                        .build();

        businessBankingService.notifyEvent(processCode, payload);
    }

}
