package p1.client.server.utils.networking.service;

import p1.client.server.domain.exception.ServerException;
import p1.client.server.utils.networking.protocol.request.RequestLogin;
import p1.client.server.utils.networking.protocol.request.RequestLogout;
import p1.client.server.utils.networking.protocol.request.RequestReserveTicket;
import p1.client.server.utils.networking.protocol.response.ResponseLogin;
import p1.client.server.utils.networking.protocol.response.ResponseLogout;
import p1.client.server.utils.networking.protocol.response.ResponseReserveTicket;


public interface Service {
    ResponseLogin login(RequestLogin requestLogin, Observer observer) throws ServerException;

    ResponseLogout logout(RequestLogout requestLogout) throws ServerException;

    ResponseReserveTicket reserveTicket(RequestReserveTicket requestReserveTicket) throws ServerException;
}
