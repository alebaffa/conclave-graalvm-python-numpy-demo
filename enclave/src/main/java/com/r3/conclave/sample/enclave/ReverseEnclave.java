package com.r3.conclave.sample.enclave;

import com.r3.conclave.enclave.Enclave;
import com.r3.conclave.mail.EnclaveMail;
import java.io.File;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * Simply reverses the bytes that are passed in.
 */
public class ReverseEnclave extends Enclave {
    // We store the previous result to showcase that the enclave internals can be examined in a mock test.
    byte[] previousResult;

    @Override
    protected byte[] receiveFromUntrustedHost(byte[] bytes) {
        // This is used for host->enclave calls so we don't have to think about authentication.
        final var input = new String(bytes);
        var result = reverse(input).getBytes();
        previousResult = result;

        return result;
    }

    private static String reverse(String input) {
        Context ctx = Context.newBuilder().allowAllAccess(true).build();
        try {
            File fibCal = new File("./heartAnalysis.py");
            ctx.eval(Source.newBuilder("python", fibCal).build());
            
            Value hearAnalysisFn = ctx.getBindings("python").getMember("heartAnalysis");

            Value heartAnalysisReport = hearAnalysisFn.execute();
            System.out.println("Average age of people who are getting level 3 and greater chest pain :" + heartAnalysisReport.toString());

        }   catch (Exception e) {
            System.out.println("Exception : " );
            e.printStackTrace();
        }
        return "Random string";
    }

    @Override
    protected void receiveMail(EnclaveMail mail, String routingHint) {
        // This is used when the host delivers a message from the client.
        // First, decode mail body as a String.
        final var stringToReverse = new String(mail.getBodyAsBytes());
        // Reverse it and re-encode to UTF-8 to send back.
        final var reversedEncodedString = reverse(stringToReverse).getBytes();
        // Get the post office object for responding back to this mail and use it to encrypt our response.
        final var responseBytes = postOffice(mail).encryptMail(reversedEncodedString);
        postMail(responseBytes, routingHint);
    }
}
