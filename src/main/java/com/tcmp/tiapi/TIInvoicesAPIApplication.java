package com.tcmp.tiapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TIInvoicesAPIApplication {
  public static void main(String[] args) {
    SpringApplication.run(TIInvoicesAPIApplication.class, args);
  }
}
