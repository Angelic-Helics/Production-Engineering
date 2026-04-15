@E2E
Feature: todo management
  Scenario: client creates a user and todos
    Given a user named Han with email han@rebels.org
    When the client creates a todo "Fix the hyperdrive" for han@rebels.org
    And the client creates a todo "Pay off Jabba" for han@rebels.org
    Then the client can retrieve 2 todos for han@rebels.org

  Scenario: reassign todo and mark as done
    Given a user named Luke with email luke@rebels.org
    And a user named Leia with email leia@rebels.org
    When the client creates a todo "Destroy the Death Star" for luke@rebels.org
    And the client reassigns the todo to leia@rebels.org
    And the client marks the todo as done
    Then the client can retrieve 1 todo for leia@rebels.org
    And the todo "Destroy the Death Star" for leia@rebels.org is marked as done
