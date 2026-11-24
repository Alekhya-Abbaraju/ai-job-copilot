
package com.alekhya.resume_service.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ResumeTextExtractionService {

    public String extractText(InputStream inputStream)
            throws IOException {

        try (InputStream stream = inputStream) {

            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();

            AutoDetectParser parser = new AutoDetectParser();

            parser.parse(stream, handler, metadata, context);

            return handler.toString().trim();

        } catch (TikaException | SAXException exception) {

            throw new IOException(
                    "Unable to extract text from the uploaded resume.",
                    exception
            );
        }
    }
}
