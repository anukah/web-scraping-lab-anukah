 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebScraper {
    public static void main(String[] args) {
        final String url = "https://www.bbc.com";

    }

    private static Data scrapeData(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Data data = new Data();
        data.setTitle(doc.title());

        List<String> headings = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Elements elements = doc.select("h" + i);
            for (Element h : elements) {
                headings.add("h" + i + ": " + h.text());
            }
        }
        data.setHeadings(headings);

        List<String> anchorLinks = new ArrayList<>();
        Elements linkElements = doc.select("a[href]");
        for (Element link : linkElements) {
            String href = link.attr("abs:href");
            String text = link.text();
            anchorLinks.add(text + " => " + href);
        }
        data.setLinks(anchorLinks);

        return data;
    }

    static class Data{

        private String title;
        private List<String> headings;
        private List<String> links;

        public void setTitle(String title) {
            this.title = title;
        }

        public void setHeadings(List<String> headings) {
            this.headings = headings;
        }

        public void setLinks(List<String> links) {
            this.links = links;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getHeadings() {
            return headings;
        }

        public List<String> getLinks() {
            return links;
        }

        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("Title: ").append(title).append("\n\n");

            sb.append("Headings:\n");
            for (String heading : headings) {
                sb.append(" - ").append(heading).append("\n");
            }

            sb.append("\nLinks:\n");
            for (String link : links) {
                sb.append(" - ").append(link).append("\n");
            }

            return sb.toString();
        }

    }

}
