package ru.effectivegroup.client.algoil;

import java.util.Objects;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/Instrument.class */
public class Instrument implements Comparable<Instrument> {
    private String code;
    private String fullName;

    public Instrument() {
    }

    public Instrument(String code, String fullName) {
        this.code = code;
        this.fullName = fullName;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Instrument that = (Instrument) o;
        return Objects.equals(this.code, that.code) && Objects.equals(this.fullName, that.fullName);
    }

    public int hashCode() {
        return Objects.hash(this.code, this.fullName);
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override // java.lang.Comparable
    public int compareTo(Instrument o) {
        return this.fullName.compareTo(o.fullName);
    }
}
