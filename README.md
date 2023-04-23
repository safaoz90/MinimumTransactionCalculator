# MinTransactionCalculator
This project is a web application that calculates the minimum number of transactions in a poker game. It is built using Java and the Spring Boot framework. Currently deployed on Azure https://game-transactions.azurewebsites.net/

## Features
- Calculate the minimum number of transactions for a poker game
- User-friendly web interface
- Fast and efficient calculation

## Components
- [Controller](https://github.com/safaoz90/MinimumTransactionCalculator/blob/master/src/main/java/com/pokerapp/mintransactioncalculator/PokerConroller.java): RESTful controller for  minimum transaction calculator application.
- [GameService](https://github.com/safaoz90/MinimumTransactionCalculator/blob/master/src/main/java/com/pokerapp/mintransactioncalculator/GameService.java): Service component for managing game setting, users, balances and transactions.
- [GameHelper](https://github.com/safaoz90/MinimumTransactionCalculator/blob/master/src/main/java/com/pokerapp/mintransactioncalculator/GameHelper.java): Contains core minimum transaction algorithm based on [optimal account balancing](https://cheonhyangzhang.gitbooks.io/leetcode-solutions/content/solutions-451-500/465-optimal-account-balancing.html) problem and pokernow ledger parser. 
- [index.html](https://github.com/safaoz90/MinimumTransactionCalculator/blob/master/src/main/resources/static/index.html): Main page to send/receive rest api calls


## Requirements
- Java Development Kit (JDK) 8 or later
- Maven 3.6 or later

## Setup and Run
1. Clone the repository:
 `git clone https://github.com/safaoz90/MinimumTransactionCalculator.git`
2. Build the project using Maven:
 `mvn clean install`
3. Run the application:
 `mvn spring-boot:run`
4. Open a web browser and navigate to:
 `http://localhost:8080`
5. Use the web interface to calculate the minimum number of transactions in a poker game.

## Contributing
1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Make your changes and commit them to your branch.
4. Push your branch to your fork.
5. Create a pull request from your fork to the original repository.

## License
This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).
