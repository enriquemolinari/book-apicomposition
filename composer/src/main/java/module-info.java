module composer {
    requires spring.context;
    requires spring.web;
    requires org.apache.tomcat.embed.core;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.beans;
    requires spring.core;
    requires java.net.http;

    requires requestparticipant;
    requires spring.webmvc;

    opens apicomposer.impl;
    opens apicomposer.main;
    opens apicomposer.web;
}