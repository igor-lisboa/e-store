Feature: Product creation and stock management
  Administrators can create products with valid details and make stock available.

  Scenario Outline: Create a product and add stock
    Given a product named <name> priced at <price> with category <category>
    When the administrator creates the product and adds 5 units of stock
    Then the product has 5 units available and retains its submitted details

    Examples:
      | name                | price  | category               |
      | "Scenario Game"     | 149.87 | "XBOX_GAMES"           |
      | "Scenario Console"  | 266.00 | "XBOX_CONSOLES"        |
      | "Scenario Wii Game" | 9.90   | "WII_GAMES"            |
      | "Scenario Wii"      | 118.00 | "WII_CONSOLES"         |
