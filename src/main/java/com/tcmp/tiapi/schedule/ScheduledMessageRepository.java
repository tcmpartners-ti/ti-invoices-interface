package com.tcmp.tiapi.schedule;

import com.tcmp.tiapi.schedule.model.ScheduledMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledMessageRepository extends JpaRepository<ScheduledMessage, String> {}
