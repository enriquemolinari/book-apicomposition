module composer {
    requires spring.context;
    requires spring.web;
    requires org.apache.tomcat.embed.core;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.beans;
    requires spring.core;
    requires java.net.http;
    requires spring.aop;

    requires requestparticipant;
    requires spring.webmvc;
    requires org.slf4j;
    requires io.github.resilience4j.annotations;
//    requires io.github.resilience4j.circuitbreaker;
//    requires io.github.resilience4j.all;
//    requires io.github.resilience4j.annotations;

    opens apicomposer.impl;
    opens apicomposer.main;
    opens apicomposer.web;
}