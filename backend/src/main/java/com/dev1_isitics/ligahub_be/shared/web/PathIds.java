package com.dev1_isitics.ligahub_be.shared.web;

/**
 * Route patterns for ids in the URL (V8): only positive integers written with digits and without
 * leading zeros match. Anything else ({@code abc}, {@code 0}, {@code -1}, {@code 01}) matches no
 * route and is answered with 404; values above {@link Integer#MAX_VALUE} fail the conversion and
 * are also answered with 404 by the global exception handler.
 */
public final class PathIds {

  public static final String ID = "{id:[1-9][0-9]*}";

  private PathIds() {}
}
