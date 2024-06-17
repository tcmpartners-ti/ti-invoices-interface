package com.tcmp.tiapi.invoice.repository.redis;

import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkCreateInvoicesFileInfoRepository
    extends CrudRepository<BulkCreateInvoicesFileInfo, String> {}
