package runge.fbi;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;

public interface FBIService
{
    @GET("/wanted/v1/list")
    Observable<MostWanted> getMostWanted();

}
