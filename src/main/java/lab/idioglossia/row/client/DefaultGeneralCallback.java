package lab.idioglossia.row.client;

import lab.idioglossia.row.client.callback.GeneralCallback;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultGeneralCallback<E> implements GeneralCallback<E> {
    @Override
    public <E> void onMessage(E e) {
        log.warn("Implement general callback to get messages published from server into no specific channel");
    }
}
