/*
 * Copyright (C) 2012, 2013 Andreas Halle
 *
 * This file is part of pplex.
 *
 * pplex is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pplex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public license
 * along with pplex. If not, see <http://www.gnu.org/licenses/>.
 */
package output;

/**
 * Class containing output-methods specifically
 * for the {@code LP} object.
 *
 * @author  Andreas Halle
 * @see     model.LP
 */
final class OLP {
    /*
     * Insert an element in the front of an array.
     */
    static String[] insert(String[] arr, String element) {
        String[] newArr = new String[arr.length + 1];
        newArr[0] = element;

        for (int i = 0; i < arr.length; i++) {
            newArr[i+1] = arr[i];
        }
        return newArr;
    }



    /*
     * Return the length of the longest
     * String in the given array.
     */
    static int longest(String[] arr) {
        int max = 1;
        for (String s : arr) {
            int len = s.length();

            if (len > max) {
                max = len;
            }
        }
        return max;
    }
}
