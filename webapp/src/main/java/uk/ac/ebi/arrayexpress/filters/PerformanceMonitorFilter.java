package uk.ac.ebi.arrayexpress.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceMonitorFilter implements javax.servlet.Filter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpServletRequest request =(HttpServletRequest) req;
		long time = System.nanoTime();
		String methInvoked=request.getRequestURL().append(
                null != request.getQueryString() ? "?" + request.getQueryString() : ""
            ).toString();
		chain.doFilter(request, resp);
	
		double ms = (System.nanoTime() - time) / 1000000d;
		logger.info("<RS>The request [{}] took [{}]ms <RE>\n", methInvoked, ms);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}
	

}
