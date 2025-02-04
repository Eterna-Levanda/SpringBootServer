package and.learn.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebFilter(value = "/Controller/*", filterName = "FilterController")
public class FilterController implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //su tutte le operation catturate dal filtro aggiungo un parametro nell'header della response
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.setHeader("ParamHeaderController", "Questo parametro è impostato sulle operation col pattern Controller/* " +
                "anche sulle operation che vanno in errore prima ancora di entrare nel metodo");

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
