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

public class FakeForeignContext extends BaseContext {

    private final String foreignPersonIdentifier;

    private final String firstNames;

    private final String familyName;

    private final String dateOfBirth;

    private final String identityAssuranceLevel = "http://uid.vrk.fi/LoA/0";

    public FakeForeignContext(String foreignPersonIdentifier, String firstNames, String familyName, String dateOfBirth) {
        this.foreignPersonIdentifier = foreignPersonIdentifier;
        this.firstNames = firstNames;
        this.familyName = familyName;
        this.dateOfBirth = dateOfBirth;
    }


    public String getForeignPersonIdentifier() {
        return this.foreignPersonIdentifier;
    }

    public String getFirstNames() {
        return this.firstNames;
    }

    public String getFamilyName() {
        return this.familyName;
    }

    public String getDateOfBirth() {
        return this.dateOfBirth;
    }

    public String getIdentityAssuranceLevel() {
        return identityAssuranceLevel;
    }

}
