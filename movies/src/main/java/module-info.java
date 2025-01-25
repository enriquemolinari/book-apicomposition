module movies {
    requires requestparticipant;
    requires com.google.gson;
    requires java.net.http;

    opens movies.participants;
}