/**
 * The MIT License
 * Copyright (c) 2015 Population Register Centre
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.vm.kapa.identification.shibboleth.extauthn.context;

import org.opensaml.messaging.context.BaseContext;

public class FakeContext extends BaseContext {

    private final String satu;

    private final String hetu;

    private final String issuerCN;

    private final String cn;

    private final String personIdentifier;

    private final String firstName;

    private final String familyName;

    private final String dateOfBirth;

    public FakeContext(String satu, String hetu, String issuerCN, String cn, String personIdentifier, String firstName, String familyName, String dateOfBirth) {
        this.satu = satu;
        this.hetu = hetu;
        this.issuerCN = issuerCN;
        this.cn = cn;
        this.personIdentifier = personIdentifier;
        this.firstName = firstName;
        this.familyName = familyName;
        this.dateOfBirth = dateOfBirth;
    }

    public String getSatu() {
        return satu;
    }

    public String getHetu() {
        return hetu;
    }

    public String getIssuerCN() {
        return issuerCN;
    }

    public String getCn() {
        return cn;
    }

    public String getPersonIdentifier() {
        return personIdentifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

}
