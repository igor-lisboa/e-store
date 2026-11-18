Feature: Active carts and sign-in handoff
  A visitor receives an active cart and can retain it after signing in.

  Scenario Outline: Resolve an active cart
    Given cart reference <cart>, customer <customer>, and visitor address "127.0.0.1"
    When the visitor requests an active cart
    Then the returned cart belongs to customer <customer>

    Examples:
      | cart | customer |
      | 0    | 0        |
      | 0    | 2        |
      | 543  | 2        |

  Scenario: A signed-in customer claims the visitor cart
    Given cart reference 0, customer 0, and visitor address "127.0.0.1"
    When the visitor requests an active cart
    Then the returned cart belongs to customer 0
    When customer 2 resumes the anonymous cart
    Then the returned cart belongs to customer 2
