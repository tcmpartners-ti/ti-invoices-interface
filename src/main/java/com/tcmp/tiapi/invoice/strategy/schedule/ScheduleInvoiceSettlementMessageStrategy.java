package com.tcmp.tiapi.invoice.strategy.schedule;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceSettlementFlowStrategy;
import com.tcmp.tiapi.schedule.ScheduledMessageRepository;
import com.tcmp.tiapi.schedule.model.MessageStatus;
import com.tcmp.tiapi.schedule.model.ScheduledMessage;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.ti.route.schedule.ScheduleIncomingStrategy;
import com.tcmp.tiapi.ti.utils.MessageUtils;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.StringReader;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleInvoiceSettlementMessageStrategy implements ScheduleIncomingStrategy {
  private final InvoiceRepository invoiceRepository;
  // Inject strategy to avoid sending the message to the queue and receiving it back just to be
  // processed by this strategy.
  private final InvoiceSettlementFlowStrategy invoiceSettlementFlowStrategy;
  private final ScheduledMessageRepository scheduledMessageRepository;

  private final UUIDGenerator uuidGenerator;
  private final Clock clock;

  @Value("${config.schedule-messages}")
  private Boolean shouldScheduleMessages;

  @Override
  public void handleIncomingMessage(String message) {
    boolean shouldSendToQueue = Boolean.FALSE.equals(shouldScheduleMessages);
    if (shouldSendToQueue) {
      parseAndHandleServiceRequest(message);
      return;
    }

    LocalDate deliverOn = getSettlementDateFromSettlementMessage(message);
    boolean shouldProcessToday = deliverOn.isEqual(LocalDate.now(clock));
    if (shouldProcessToday) {
      parseAndHandleServiceRequest(message);
      return;
    }

    saveMessageAsScheduled(message, deliverOn);
  }

  private void parseAndHandleServiceRequest(String message) {
    AckServiceRequest<?> serviceRequest = parseMessageAsServiceRequest(message);
    invoiceSettlementFlowStrategy.handleServiceRequest(serviceRequest);
  }

  private AckServiceRequest<?> parseMessageAsServiceRequest(String message) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(AckServiceRequest.class);
      return (AckServiceRequest<?>)
          jaxbContext.createUnmarshaller().unmarshal(new StringReader(message));
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

  private LocalDate getSettlementDateFromSettlementMessage(String message) {
    String masterReference = MessageUtils.extractFieldFromMessage("MasterRef", message);

    return invoiceRepository
        .findByProductMasterMasterReference(masterReference)
        .map(InvoiceMaster::getSettlementDate)
        .orElseGet(
            () -> {
              String paymentValueDate =
                  MessageUtils.extractFieldFromMessage("PaymentValueDate", message);
              return LocalDate.parse(paymentValueDate);
            });
  }

  private void saveMessageAsScheduled(String message, LocalDate deliverOn) {
    String uuid = uuidGenerator.getNewId();

    ScheduledMessage scheduledMessage =
        ScheduledMessage.builder()
            .id(uuid)
            .status(MessageStatus.PENDING)
            .deliverOn(deliverOn)
            .originalMessage(message)
            .build();

    scheduledMessageRepository.save(scheduledMessage);

    log.info("Message scheduled to be sent on = {}.", deliverOn);
  }
}
