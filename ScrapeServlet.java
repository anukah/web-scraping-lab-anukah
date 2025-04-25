import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/scrape")
public class ScrapeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String url = request.getParameter("url");

        if (url.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "URL is missing");
            return;
        }

        HttpSession session = request.getSession();
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) visitCount = 0;
        session.setAttribute("visitCount", visitCount + 1);

        Document doc = Jsoup.connect(url).get();
        Map<String, Object> scraped = new LinkedHashMap<>();

        scraped.put("title", doc.title());

        List<String> headings = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Elements elems = doc.select("h" + i);
            for (Element e : elems) {
                headings.add("h" + i + ": " + e.text());
            }
        }
        scraped.put("headings", headings);

        List<String> links = new ArrayList<>();
        Elements aTags = doc.select("a[href]");
        for (Element link : aTags) {
            links.add(link.text() + " => " + link.attr("abs:href"));
        }
        scraped.put("links", links);

        session.setAttribute("scrapedData", scraped);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Scraped Data</title></head><body>");
        out.println("<h2>Scraped Results</h2>");
        out.println("<table border='1'>");

        for (Map.Entry<String, Object> entry : scraped.entrySet()) {
            out.println("<tr><th>" + entry.getKey() + "</th><td>");
            if (entry.getValue() instanceof List) {
                out.println("<ul>");
                for (String item : (List<String>) entry.getValue()) {
                    out.println("<li>" + item + "</li>");
                }
                out.println("</ul>");
            } else {
                out.println(entry.getValue());
            }
            out.println("</td></tr>");
        }

        out.println("</table>");

        out.println("<p>You have visited this page <strong>" + (visitCount + 1) + "</strong> times.</p>");

        out.println("<br><a href='download'>Download CSV</a> | <a href='json'>Get JSON</a>");
        out.println("</body></html>");
    }
}