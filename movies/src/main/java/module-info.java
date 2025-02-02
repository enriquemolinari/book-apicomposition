module movies {
    requires requestparticipant;
    requires com.google.gson;
    requires java.net.http;

    opens movies.participants.in.shows;
    opens movies.participants.in.rates;
}