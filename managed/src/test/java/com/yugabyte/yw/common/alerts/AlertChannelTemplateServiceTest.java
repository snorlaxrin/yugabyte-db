// Copyright (c) YugaByte, Inc.

package com.yugabyte.yw.common.alerts;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

import com.yugabyte.yw.common.FakeDBApplication;
import com.yugabyte.yw.common.ModelFactory;
import com.yugabyte.yw.common.PlatformServiceException;
import com.yugabyte.yw.models.AlertChannel.ChannelType;
import com.yugabyte.yw.models.AlertChannelTemplates;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class AlertChannelTemplateServiceTest extends FakeDBApplication {

  private UUID defaultCustomerUuid;

  private AlertChannelTemplateService templateService;

  @Before
  public void setUp() {
    defaultCustomerUuid = ModelFactory.testCustomer().getUuid();
    templateService = app.injector().instanceOf(AlertChannelTemplateService.class);
  }

  @Test
  public void testCreateAndGet() {
    AlertChannelTemplates templates = createTemplates(ChannelType.Email);
    templateService.save(templates);

    AlertChannelTemplates fromDb = templateService.get(defaultCustomerUuid, ChannelType.Email);
    assertThat(fromDb, equalTo(templates));
  }

  @Test
  public void testGetOrBadRequest() {
    PlatformServiceException exception =
        assertThrows(
            PlatformServiceException.class,
            () -> {
              templateService.getOrBadRequest(defaultCustomerUuid, ChannelType.Email);
            });
    assertThat(exception.getMessage(), equalTo("No templates defined for channel type Email"));
  }

  @Test
  public void testList() {
    AlertChannelTemplates templates = createTemplates(ChannelType.Email);
    templateService.save(templates);
    AlertChannelTemplates templates2 = createTemplates(ChannelType.Slack);
    templateService.save(templates2);

    // Second customer with one channel.
    UUID newCustomerUUID = ModelFactory.testCustomer().uuid;
    AlertChannelTemplates otherCustomerTemplate =
        new AlertChannelTemplates()
            .setType(ChannelType.Email)
            .setCustomerUUID(newCustomerUUID)
            .setTextTemplate("qwewqe");
    templateService.save(otherCustomerTemplate);

    List<AlertChannelTemplates> customerTemplates = templateService.list(defaultCustomerUuid);
    assertThat(customerTemplates, containsInAnyOrder(templates, templates2));
  }

  @Test
  public void testUpdate() {
    AlertChannelTemplates templates = createTemplates(ChannelType.Email);
    templateService.save(templates);

    AlertChannelTemplates updated =
        new AlertChannelTemplates()
            .setCustomerUUID(defaultCustomerUuid)
            .setType(ChannelType.Email)
            .setTextTemplate("newTemplate");
    AlertChannelTemplates updateResult = templateService.save(updated);
    assertThat(updateResult, equalTo(updated));

    AlertChannelTemplates updatedFromDb =
        templateService.get(defaultCustomerUuid, ChannelType.Email);
    assertThat(updatedFromDb, equalTo(updated));
  }

  private AlertChannelTemplates createTemplates(ChannelType type) {
    return createTemplates(defaultCustomerUuid, type);
  }

  public static AlertChannelTemplates createTemplates(UUID customerUuid, ChannelType type) {
    return new AlertChannelTemplates()
        .setCustomerUUID(customerUuid)
        .setType(type)
        .setTitleTemplate("titleTemplate")
        .setTextTemplate("textTemplate");
  }
}