package jenkins.plugins.elanceodesk.workplace.notifier;

import hudson.model.TaskListener;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Phase.class, HttpWorker.class})
public class HttpWorkerTest {

	
	
	AbstractBuild buildMock = Mockito.mock(AbstractBuild.class);
	TaskListener listenerMock = Mockito.mock(TaskListener.class);
	WebhookJobProperty property;
	private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.create();
	@Before
	public void setup() throws IOException {
	}
	
	@Test
	public void testSendingMultipleWebhooks() throws IOException, InterruptedException {
		ExecutorService executorService = Executors.newCachedThreadPool();
		HttpWorker worker = new HttpWorker("http://localhost:8000/test", "test", 30000, Mockito.mock(PrintStream.class));
		executorService.submit(worker);
		
	}
	
	static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            InputStream is = t.getRequestBody();
            String theString = IOUtils.toString(is, "UTF-8");
            
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
