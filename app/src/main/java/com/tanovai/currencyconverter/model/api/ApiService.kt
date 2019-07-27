import com.tanovai.currencyconverter.model.RatesResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("latest")
    fun getRates(@Query("base") base: String): Observable<RatesResponse>
}