package com.tcmp.tiapi.customer.mapper;

import com.tcmp.tiapi.customer.dto.csv.CustomerCreationCSVRow;
import com.tcmp.tiapi.customer.dto.ti.Account;
import com.tcmp.tiapi.customer.dto.ti.Customer;
import com.tcmp.tiapi.customer.dto.ti.CustomerItemRequest;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.dto.MaintenanceType;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.dto.TIService;
import com.tcmp.tiapi.ti.dto.request.ReplyFormat;
import com.tcmp.tiapi.ti.dto.request.ServiceRequest;
import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class CustomerMapper {
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @Autowired private TIServiceRequestWrapper wrapper;

  @Mapping(target = "maintenanceType", constant = "")
  @Mapping(target = "maintainedInBackOffice", constant = "N")
  @Mapping(target = "sourceBankingBusiness", source = "sourceBankingBusiness")
  @Mapping(target = "mnemonic", source = "mnemonic")
  @Mapping(target = "number", source = "number")
  @Mapping(target = "type", source = "type")
  @Mapping(target = "fullName", source = "fullName")
  @Mapping(target = "shortName", source = "shortName")
  @Mapping(target = "bankCode", source = "bankCode")
  @Mapping(target = "addressDetails", expression = "java(mapCsvRowToAddressDetails(row))")
  @Mapping(target = "otherDetails.corporateAccess", constant = "CorporateChannels")
  abstract Customer mapCsvRowToCustomer(CustomerCreationCSVRow row);

  public List<Customer.AddressDetail> mapCsvRowToAddressDetails(CustomerCreationCSVRow row) {
    return List.of(
        Customer.AddressDetail.builder()
            .addressType("P")
            .nameAndAddress(row.getAddress())
            .phone(row.getPhone())
            .email(row.getEmail())
            .build());
  }

  @Mapping(target = "maintenanceType", constant = "")
  @Mapping(target = "maintainedInBackOffice", constant = "N")
  @Mapping(target = "customer.sourceBankingBusiness", source = "sourceBankingBusiness")
  @Mapping(target = "customer.mnemonic", source = "mnemonic")
  @Mapping(target = "type", source = "accountType")
  @Mapping(target = "currency", source = "accountCurrency")
  @Mapping(target = "externalAccount", source = "accountNumber")
  @Mapping(target = "dateOpened", source = "accountDateOpened", dateFormat = DATE_FORMAT)
  abstract Account mapCsvRowToAccount(CustomerCreationCSVRow row);

  public ServiceRequest<CustomerItemRequest> mapCustomerAndAccountToBulkRequest(
      MaintenanceType maintenanceType, CustomerCreationCSVRow row) {
    Customer customer = mapCsvRowToCustomer(row);
    customer.setMaintenanceType(maintenanceType.value());

    Account account = mapCsvRowToAccount(row);
    account.setMaintenanceType(maintenanceType.value());

    CustomerItemRequest customerItemRequest =
        new CustomerItemRequest(
            wrapper.wrapRequest(
                TIService.TRADE_INNOVATION,
                TIOperation.CREATE_CUSTOMER,
                ReplyFormat.STATUS,
                null,
                customer),
            wrapper.wrapRequest(
                TIService.TRADE_INNOVATION,
                TIOperation.CREATE_ACCOUNT,
                ReplyFormat.STATUS,
                null,
                account));

    return wrapper.wrapRequest(
        TIService.TRADE_INNOVATION_BULK,
        TIOperation.ITEM,
        ReplyFormat.STATUS,
        null,
        customerItemRequest);
  }
}
