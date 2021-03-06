package org.assertj.core.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * the same as {@link SoftAssertions}, but with the following differences:
 *
 * First, it's a junit rule, which can be used as follows (without the assertAll())
 *
 * <pre>
 *   public class SoftlyTest {
 *
 *     &#064;Rule
 *     public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
 *
 *     &#064;Test
 *     public void testSoftly() throws Exception {
 *       softly.assertThat(1).isEqualTo(2);
 *       softly.assertThat(Lists.newArrayList(1, 2)).containsOnly(1, 2);
 *     }
 *  }
 * </pre>
 *
 * Second, the failures are recognized by IDE's (like IntelliJ IDEA) which open a comparison window.
 */
public class JUnitSoftAssertions extends AbstractSoftAssertions<JUnitSoftAssertions.ErrorCollector> implements TestRule {

    /**
     * Collects error messages of all AssertionErrors thrown by the proxied method.
     */
    static class ErrorCollector implements MethodInterceptor {

        private List<Throwable> errors = new ArrayList<Throwable>();

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            try {
                proxy.invokeSuper(obj, args);
            } catch (AssertionError e) {
                errors.add(e);
            }
            return obj;
        }

        public void verify() throws Throwable {
            MultipleFailureException.assertEmpty(errors);
        }
    }

    public JUnitSoftAssertions() {
        super(new ErrorCollector());
    }

    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                base.evaluate();
                collector.verify();
            }
        };
    }

}
