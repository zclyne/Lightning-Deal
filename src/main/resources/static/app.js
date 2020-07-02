var stompClient = null;

function setConnected(connected) {
    $("#login").prop("disabled", connected);
    if (connected) {
        $("#notification").show();
    }
    else {
        $("#notification").hide();
    }
    $("#notifications").html("");
}

function login() {
    // login
    var username = $("#username").val();
    var password = $("#password").val();
    $.ajax({
        contentType: "application/json",
        data: JSON.stringify({
            "username": username,
            "password": password
        }),
        dataType: "json",
        url:"/login",
        type: "POST",
        success: function(response) {
            console.log(response);
        }
    });
}

function connectWS() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/notification', function (notification) {
            showNotification(JSON.parse(notification.body).content);
        });
    });
    // make a REST API call to list all notifications
    $.ajax({
        url:"/notification/list",
        success: function(response) {
            console.log(response);
            let notifications = response.data;
            console.log(notifications);
            for (let i = 0; i < notifications.length; i++) {
                let notification = notifications[i];
                showNotification(notification.content);
            }
        }
    });
}

function showNotification(notification) {
    $("#notifications").append("<tr><td>" + notification + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#login" ).click(function() { login(); });
    $("#connect").click(function () {
        connectWS();
    });
});