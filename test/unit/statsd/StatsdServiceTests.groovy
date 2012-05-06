package statsd

import grails.test.mixin.*

import grails.plugin.statsd.StatsdService
import org.junit.Test
import org.junit.Before
import org.gmock.GMockTestCase
import org.apache.commons.pool.ObjectPool
import grails.plugin.statsd.StatsdClient

@TestFor(StatsdService)
class StatsdServiceTests extends GMockTestCase {

    StatsdService service
    def pool
    def client


    @Before
    void setup() {
        service = new StatsdService()
        // Using gmock because mockFor does not support classes with constructors
        pool = mock(ObjectPool)
        client = mock(StatsdClient)
    }

    private void runTestWithPool(Closure closure) {
        pool.borrowObject().returns(client)
        pool.returnObject(client)
        play {
            service.statsdPool = pool
            closure.call()
        }
    }

    @Test
    public void testTimingClosureWithDefaults() {
        String metric = 'test234'
        client.send(1.0, "${metric}:0|ms")
        runTestWithPool {
            def value = service.withTimer(metric) {
                1+1
            }
            assert value == 2
        }
    }

    @Test
    public void testTimingClosure() {
        String metric = 'test234'
        double sampling = 0.8
        client.send(0.8, "${metric}:0|ms")
        runTestWithPool {
            def h = 'hello'
            def w = "world"
            def value = service.withTimer(metric, sampling) {
                "${h} ${w}"
            }
            assert value == "hello world"
        }
    }

    @Test
    public void testTimingWithDefaultSampling() {
        String metric = 'sadfsdfsadfasdf'
        int timeInMS = 1234
        client.send(1.0, "${metric}:${timeInMS}|ms")
        runTestWithPool {
            service.timing(metric, timeInMS)
        }
    }

    @Test
    public void testTiming() {
        String metric = 'asdfsadfsadfasdfsadf'
        int timeInMS = 1234
        double sampleRate = 1.5
        client.send(sampleRate, "${metric}:${timeInMS}|ms")
        runTestWithPool {
            service.timing(metric, timeInMS, sampleRate)
        }
    }

    @Test
    public void testDecrementWithDefaults() {
        String metric = 'asdfsadfsadfasdfsadf'
        client.send(1.0, "${metric}:-1|c")
        runTestWithPool {
            service.decrement(metric)
        }
    }


    @Test
    public void testDecrementWithDefaultSampling() {
        String metric = 'test'
        int magnitude = 4
        client.send(1.0, "${metric}:-4|c")
        runTestWithPool {
            service.decrement(metric, magnitude)
        }
    }

    @Test
    public void testDecrement() {
        String metric = 'test'
        int magnitude = -3
        double sampling = 1.3
        client.send(sampling, "${metric}:-3|c")
        runTestWithPool {
            service.decrement(metric, magnitude, sampling)
        }
    }

    @Test
    public void testIncrementWithDefaults() {
        String metric = 'adasdfsad'
        client.send(1.0, "${metric}:1|c")
        runTestWithPool {
            service.increment(metric)
        }
    }


    @Test
    public void testIncrementWithDefaultSampling() {
        String metric = 'test23'
        int magnitude = 4
        client.send(1.0, "${metric}:4|c")
        runTestWithPool {
            service.increment(metric, magnitude)
        }
    }

    @Test
    public void testIncrement() {
        String metric = 'test'
        int magnitude = 3
        double sampling = 1.3
        client.send(sampling, "${metric}:3|c")
        runTestWithPool {
            service.increment(metric, magnitude, sampling)
        }
    }
}
