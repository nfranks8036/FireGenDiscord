package net.noahf.firegen.discord.utilities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ListDiff<T> {

    public static <T> ListDiff<T> compare(List<T> oldList, List<T> newList) {
        List<T> added = new ArrayList<>(newList);
        added.removeAll(oldList);

        List<T> removed = new ArrayList<>(oldList);
        removed.removeAll(newList);

        return new ListDiff<>(added, removed);
    }

    private final @Getter List<T> added;
    private final @Getter List<T> removed;

    public List<T> getDifferences() {
        List<T> differences = new ArrayList<>(added);
        differences.addAll(removed);
        return differences;
    }

    @Override
    public String toString() {
        return "ListDiff[added=" + added + ", removed=" + removed + "]";
    }
}
