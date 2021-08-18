package com.fa993.retrieval;

import com.fa993.core.exceptions.MangaFetchingException;
import com.fa993.core.exceptions.PageProcessingException;
import com.fa993.core.pojos.Source;
import com.fa993.retrieval.pojos.MangaDTO;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface SourceScrapper {

    class Reference<T> {
        public T ref;

        public Reference(T reference) {
            this.ref = reference;
        }

    }

    default void extractWithTrim(String haystack, Collection<String> output, Set<String> restricted, char... delimiters) {
        int j = 0;
        for (int i = 0; i < haystack.length(); i++) {
            for (int k = 0; k < delimiters.length; k++) {
                if (haystack.charAt(i) == delimiters[k]) {
                    String x = haystack.substring(j, i).strip();
                    if (!(x.isBlank() || x.isEmpty() || restricted.contains(x))) {
                        output.add(x);
                    }
                    j = i + 1;
                    break;
                }
            }
        }
        String x = haystack.substring(j).strip();
        if (!(x.isBlank() || x.isEmpty() || restricted.contains(x))) {
            output.add(x);
        }
    }

    public MangaDTO getManga(String url) throws MangaFetchingException;

    public Integer getCompleteNumberOfPages();
    
    public void getLiterallyEveryLink(int x, Consumer<String> onProcessed) throws PageProcessingException;

    public Integer getNumberOfPagesToWatch();
    
    public void watch(int x, Consumer<String> onProcessed) throws PageProcessingException;

    public Source getSource();

    public List<String> getAllGenre();

}
