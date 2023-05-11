package runge.fbi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
public class FBIFrame extends JFrame
{
    public FBIFrame() throws IOException {

        setSize(500, 500);
        setTitle("FBI MOST WANTED");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JButton nextButton = new JButton("Next");
        JButton prevButton = new JButton("Previous");


        //add name of POI here?
        JLabel label = new JLabel();
        label.setFont(new Font("Serif", Font.PLAIN, 36));
        label.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(label, BorderLayout.CENTER);

        setContentPane(mainPanel);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor()
                {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response response = chain.proceed(chain.request());

                        if (response.isSuccessful())
                        {

                            String text = response.body().string()
                                    .replaceAll("<[^>]*>", "");
//                                    .replaceAll("<html><body>", "")
//                                    .replaceAll("</body></html>", "")
//                                    .replaceAll("\r","")
//                                    .replaceAll("\n","");

                            Response.Builder builder = response.newBuilder()
                                    .body(ResponseBody.create(MediaType.get("application/json"), text));

                            return builder.build();
                        }

                        return response;
                    }
                })
                .build();

        Gson gson = new GsonBuilder().setLenient()
                .disableHtmlEscaping()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.fbi.gov/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();


        FBIService service = retrofit.create(FBIService.class);

        Disposable disposable = service.getMostWanted()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(
                        mostWanted ->
                        {
                            String person = mostWanted.getItems()[0].getDetails();
                            label.setText(person);
                        },

                        Throwable::printStackTrace
                );

    }
}
