module users {
    requires requestparticipant;
    requires com.google.gson;
    requires java.net.http;

    opens users.participants.in;
    opens users.participants.in.rates;
    opens users.participants.in.profile;
    opens users.participants.in.shows.sale;
}