package com.tcmp.tiapi.customer.mapper;

import com.tcmp.tiapi.customer.dto.csv.CustomerCreationCSVRow;
import com.tcmp.tiapi.customer.dto.ti.Account;
import com.tcmp.tiapi.customer.dto.ti.Customer;
import com.tcmp.tiapi.customer.dto.ti.CustomerType;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.dto.MaintenanceType;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.dto.TIService;
import com.tcmp.tiapi.ti.dto.request.*;
import java.util.ArrayList;
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

  @Mapping(target = "maintenanceType", ignore = true)
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

  @Mapping(target = "maintenanceType", ignore = true)
  @Mapping(target = "maintainedInBackOffice", constant = "N")
  @Mapping(target = "customer.sourceBankingBusiness", source = "sourceBankingBusiness")
  @Mapping(target = "customer.mnemonic", source = "mnemonic")
  @Mapping(target = "type", source = "accountType")
  @Mapping(target = "currency", source = "accountCurrency")
  @Mapping(target = "externalAccount", source = "accountNumber")
  @Mapping(target = "dateOpened", source = "accountDateOpened", dateFormat = DATE_FORMAT)
  abstract Account mapCsvRowToAccount(CustomerCreationCSVRow row);

  @Mapping(target = "maintenanceType", ignore = true)
  @Mapping(target = "maintainedInBackOffice", constant = "N")
  @Mapping(target = "type", source = "type")
  @Mapping(target = "description", source = "type")
  @Mapping(target = "qualifier", constant = "C")
  abstract CustomerType mapCsvRowToCustomerType(CustomerCreationCSVRow row);

  public MultiItemServiceRequest mapCustomerAndAccountToBulkRequest(
      MaintenanceType maintenanceType, CustomerCreationCSVRow row) {
    CustomerType type = mapCsvRowToCustomerType(row);
    type.setMaintenanceType(maintenanceType.value());

    Customer customer = mapCsvRowToCustomer(row);
    customer.setMaintenanceType(maintenanceType.value());

    Account account = mapCsvRowToAccount(row);
    account.setMaintenanceType(maintenanceType.value());

    ServiceRequest<CustomerType> customerTypeRequest =
        wrapper.wrapRequest(
            TIService.TRADE_INNOVATION,
            TIOperation.CREATE_CUSTOMER_TYPE,
            ReplyFormat.STATUS,
            null,
            type);
    ServiceRequest<Customer> customerRequest =
        wrapper.wrapRequest(
            TIService.TRADE_INNOVATION,
            TIOperation.CREATE_CUSTOMER,
            ReplyFormat.STATUS,
            null,
            customer);
    ServiceRequest<Account> accountRequest =
        wrapper.wrapRequest(
            TIService.TRADE_INNOVATION,
            TIOperation.CREATE_ACCOUNT,
            ReplyFormat.STATUS,
            null,
            account);

    RequestHeader requestHeader =
        RequestHeader.builder()
            .service(TIService.TRADE_INNOVATION_BULK.getValue())
            .operation(TIOperation.ITEM.getValue())
            .replyFormat(ReplyFormat.STATUS.getValue())
            .noOverride("N")
            .correlationId(null)
            .credentials(Credentials.builder().name("TI_INTERFACE").build())
            .build();
    List<ItemRequest> itemRequests = new ArrayList<>();
    itemRequests.add(new ItemRequest(customerTypeRequest));
    itemRequests.add(new ItemRequest(customerRequest));
    itemRequests.add(new ItemRequest(accountRequest));

    return new MultiItemServiceRequest(requestHeader, itemRequests);
  }
}
