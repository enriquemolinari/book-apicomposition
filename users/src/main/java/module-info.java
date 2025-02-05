module users {
    requires requestparticipant;
    requires com.google.gson;
    requires java.net.http;

    opens users.participants.in.rates;
}