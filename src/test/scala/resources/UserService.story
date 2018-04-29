Meta:

Scenario: Get all strategies
Given the server is started
When I call get all sequential users
Then the response code should be 200
Then the response text should contain shuffle
Then the response text should contain rcb
