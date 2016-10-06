package au.org.aekos.service.metric;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.Resource;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=ApiMetricsAspectTestContext.class)
public class ApiMetricsAspectTest {

	@Autowired
	private ApiMetricsAspect objectUnderTest;
	
	@Resource(name="metricsInnerQueue")
	private BlockingQueue<MetricsQueueItem> metricsInnerQueue;
	
	/**
	 * Do we end up with the expected number of queue items after doing one of every call?
	 */
	@Test
	public void testAllCalls01() {
		objectUnderTest.afterCallingGetTraitVocab();
		// TODO add all other calls
		assertThat(metricsInnerQueue.size(), is(1));
	}
}

@Configuration
@ComponentScan(
	basePackages={"au.org.aekos.service.metric"},
	excludeFilters={
		@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=MetricsQueueWorker.class)
	})
class ApiMetricsAspectTestContext {
	
	@Bean
	public Dataset metricsDS() {
    	return DatasetFactory.create();
    }
	
	@Bean
	public Model metricsModel(Dataset metricsDS) {
		return metricsDS.getDefaultModel();
	}
	
	@Bean
    public BlockingQueue<MetricsQueueItem> metricsInnerQueue() {
    	return new LinkedBlockingDeque<>(10);
    }
	
	@Bean
	public CounterService counterService() {
		return new CounterService() {
			@Override
			public void reset(String metricName) { }
			
			@Override
			public void increment(String metricName) { }
			
			@Override
			public void decrement(String metricName) { }
		};
	}
}
