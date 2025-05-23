module shows {
    requires requestparticipant;
    requires java.net.http;
    requires com.google.gson;

    //required to be instantiated by reflections
    opens shows.participants;
    opens shows.participants.in.shows;
    opens shows.participants.in.show.sale;
    opens shows.participants.in.user.profile;
}