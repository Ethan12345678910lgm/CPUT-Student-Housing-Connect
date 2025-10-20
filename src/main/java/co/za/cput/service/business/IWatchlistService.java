package co.za.cput.service.business;

import co.za.cput.domain.business.WatchlistItem;

import java.util.List;

public interface IWatchlistService {
    WatchlistItem addToWatchlist(Long studentId, Long accommodationId);

    List<WatchlistItem> findForStudent(Long studentId);

    void removeFromWatchlist(Long studentId, Long accommodationId);
}