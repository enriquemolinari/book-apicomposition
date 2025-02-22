# Cinema Ticket System - API Composer

## Starting up API Composer modules

- `git clone https://github.com/enriquemolinari/book-apicomposition.git`
- `cd api-composer`
- To compile and install all dependencies: `./mvnw install`
- To run all tests: `./mvnw test`
- To start the service: `./mvnw exec:java`.
- Once started, you can open swagger UI:
    - http://localhost:8090/swagger-ui/index.html

## Framework Architecture

- API Composer is a framework that enables services to participate in a request/response call, allowing them to add data
  to the response sent to UI clients.
- The framework consists of two main modules: the **composer module** and the **requestparticipant module**.
    - **Composer Module**: Provides the core framework functionality, allowing services to extend it and inject their
      implementations. This enables them to contribute to a request/response call, augmenting the response data sent to
      UI clients.
    - **RequestParticipant Module**: Defines the interface that services must implement to participate in request calls.
    - Each team responsible for a microservice (users, movies, and shows) provides its implementation of the
      RequestParticipant interface to participate in specific request/response calls.
- The API Composer framework is inspired by the ViewModel Composition concepts presented
  by [Mauro Servienti](https://milestone.topics.it/series/view-model-composition.html).