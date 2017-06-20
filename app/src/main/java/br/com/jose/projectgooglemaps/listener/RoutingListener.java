package br.com.jose.projectgooglemaps.listener;

import java.util.List;

import br.com.jose.projectgooglemaps.domain.Router;

/**
 * Created by jose on 6/19/17.
 */

public interface RoutingListener {
    void onDirectionStart();
    void onDirectionSuccess(List<Router> routers);
}
