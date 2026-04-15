@E2E
Feature: order management
  Scenario: client creates a customer and orders
    Given a customer named Han with email han@rebels.org and phone +40111111111
    When the client creates an order "Fix the hyperdrive" with quantity 1 for han@rebels.org
    And the client creates an order "Pay off Jabba" with quantity 1 for han@rebels.org
    Then the client can retrieve 2 order(s) for han@rebels.org

  Scenario: mark order as ready
    Given a customer named Leia with email leia@rebels.org and phone +40222222222
    When the client creates an order "Destroy the Death Star" with quantity 1 for leia@rebels.org
    And the client marks the order as ready
    Then the client can retrieve 1 order(s) for leia@rebels.org
    And the order "Destroy the Death Star" for leia@rebels.org is marked as ready
