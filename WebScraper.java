import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebScraper {

    public static void main(String[] args) throws IOException {
        final String url = "https://www.bbc.com";
        Data data = scrapeData(url);
        System.out.println(data.format());
    }

    private static Data scrapeData(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Data data = new Data();
        data.setTitle(doc.title());

        List<String> headings = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
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

        List<ArticleData> articles = new ArrayList<>();
        Elements newsBlocks = doc.select("[data-testid=dundee-card]");

        for (Element block : newsBlocks) {
            String headline = block.select("[data-testid=card-headline]").text();

            if (!headline.isEmpty()) {
                String link = block.select("a").attr("href");
                String fullUrl = link.startsWith("http") ? link : url + link;

                try {
                    Document articlePage = Jsoup.connect(fullUrl).get();

                    String author = articlePage.select("[data-testid=byline-new-contributors] div div span").text();
                    if (author.isEmpty()) {
                        author = "Unknown";
                    }

                    String date = articlePage.select("[data-testid=byline-new] time").text();
                    if (date.isEmpty()) {
                        date = "Unknown";
                    }

                    ArticleData article = new ArticleData(headline, author, date);
                    articles.add(article);

                } catch (IOException e) {
                    System.err.println("Failed to load article page: " + fullUrl);
                }
            }
        }

        data.setArticles(articles);

        return data;
    }

    static class Data {
        private String title;
        private List<String> headings;
        private List<String> links;
        private List<ArticleData> articles;

        public void setTitle(String title) {
            this.title = title;
        }

        public void setHeadings(List<String> headings) {
            this.headings = headings;
        }

        public void setLinks(List<String> links) {
            this.links = links;
        }

        public void setArticles(List<ArticleData> articles) {
            this.articles = articles;
        }

        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("Title: ").append(title).append("\n\n");

            sb.append("Headings:\n");
            for (String heading : headings) {
                sb.append(heading).append("\n");
            }

            sb.append("\nLinks:\n");
            for (String link : links) {
                sb.append(link).append("\n");
            }

            sb.append("\nNews Articles:\n");
            for (ArticleData article : articles) {
                sb.append(article.toString()).append("\n");
            }

            return sb.toString();
        }
    }

    static class ArticleData {
        private final String headline;
        private final String author;
        private final String date;

        public ArticleData(String headline, String author, String date) {
            this.headline = headline;
            this.author = author;
            this.date = date;
        }

        public String toString() {
            return "--------------------------\n" +
                   "Headline: " + headline + "\n" +
                   "Author: " + author + "\n" +
                   "Date: " + date;
        }
    }
}