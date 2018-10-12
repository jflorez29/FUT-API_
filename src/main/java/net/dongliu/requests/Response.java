package net.dongliu.requests;


import java.io.Serializable;
import java.util.List;


/**
 * Response with transformed result
 *
 * @author Liu Dong
 */
public class Response<T> extends AbstractResponse implements Serializable {
    private static final long serialVersionUID = 5956373495731090956L;
    private final T body;

    /**
     * Create new response instance.
     * @param url the final url(redirected)
     * @param statusCode the status code
     * @param cookies the cookies
     * @param headers the headers
     * @param body the body
     */
    Response(String url, int statusCode, List<Cookie> cookies, Headers headers, T body) {
        super(url, statusCode, cookies, headers);
        this.body = body;
    }

    /**
     * Return the body part.
     * @return the body
     * @see #body()
     */
    public T getBody() {
        return body;
    }


    public String text(){
        if (body instanceof String){
            return body.toString();
        }else{
            return "";
        }
    }

    /**
     * Return the body part.
     * @return the body
     * @see #getBody()
     */
    public T body() {
        return body;
    }
}
