package br.uff.store.application.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.services.IReviewService;
import br.uff.store.infrastructure.shared.Helper;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ReviewServletTest {
    private static final int USER_ID = 22;
    private static final int OTHER_USER_ID = 99;

    private IReviewService reviewService;
    private ReviewServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() {
        reviewService = mock(IReviewService.class);
        servlet = new ReviewServlet(reviewService, new Helper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(USER_ID);
        when(session.getAttribute("productId")).thenReturn("7");
        when(request.getMethod()).thenReturn("POST");
    }

    @Test
    public void guestIsSentToSignInAndChangesNothing() throws Exception {
        when(session.getAttribute("userId")).thenReturn(null);
        when(request.getParameter("action")).thenReturn("deleteReview");

        servlet.doPost(request, response);

        verify(response).sendRedirect("account?redirect=reviews");
        verifyNoInteractions(reviewService);
    }

    @Test
    public void updatesAreAppliedToTheSessionUserEvenIfARequestNamesSomeoneElse() throws Exception {
        when(request.getParameter("action")).thenReturn("updateReview");
        when(request.getParameter("rating")).thenReturn("4");
        when(request.getParameter("title")).thenReturn("Better");
        when(request.getParameter("description")).thenReturn("After a month");
        when(request.getParameter("userId")).thenReturn(String.valueOf(OTHER_USER_ID));

        servlet.doPost(request, response);

        ArgumentCaptor<ProductReviewInputDTO> captured = ArgumentCaptor.forClass(ProductReviewInputDTO.class);
        verify(reviewService).updateReview(captured.capture());
        org.junit.Assert.assertEquals(Integer.valueOf(USER_ID), captured.getValue().getUserId());
        org.junit.Assert.assertEquals(Integer.valueOf(7), captured.getValue().getProductId());
        org.junit.Assert.assertEquals(Integer.valueOf(4), captured.getValue().getRating());
        verify(response).sendRedirect("product");
    }

    @Test
    public void deletionUsesTheSessionUserAndTheSessionProduct() throws Exception {
        when(request.getParameter("action")).thenReturn("deleteReview");
        when(request.getParameter("userId")).thenReturn(String.valueOf(OTHER_USER_ID));

        servlet.doPost(request, response);

        verify(reviewService).deleteReview(USER_ID, 7);
        verify(response).sendRedirect("product");
    }

    @Test
    public void changesRequirePostBecauseTheyAlterData() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        for (String action : new String[] {"updateReview", "deleteReview", "submitReview"}) {
            when(request.getParameter("action")).thenReturn(action);
            servlet.doGet(request, response);
        }

        verify(response, org.mockito.Mockito.times(3)).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(reviewService, never()).updateReview(any());
        verify(reviewService, never()).deleteReview(anyInt(), anyInt());
        verify(reviewService, never()).reviewProduct(any());
    }

    @Test
    public void rejectedChangeIsShownToTheUserAndNothingElseHappens() throws Exception {
        when(request.getParameter("action")).thenReturn("deleteReview");
        doThrow(new StoreException("You have no review of this product to delete.")).when(reviewService)
                .deleteReview(USER_ID, 7);

        servlet.doPost(request, response);

        verify(session).setAttribute("msg", "You have no review of this product to delete.");
        verify(response).sendRedirect("product");
    }

    @Test
    public void editFormIsPrefilledOnlyFromTheSessionUsersOwnReview() throws Exception {
        ProductReviewInputDTO own = new ProductReviewInputDTO(USER_ID, 7, 5, "Text", "Title");
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("edit")).thenReturn("1");
        when(reviewService.findUserReview(USER_ID, 7)).thenReturn(own);
        RequestDispatcher page = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("pages/product-review.jsp")).thenReturn(page);

        servlet.doGet(request, response);

        verify(request).setAttribute("review", own);
        verify(page).forward(request, response);
    }

    @Test
    public void editingWithoutAnExistingReviewReturnsToTheProductWithAMessage() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("edit")).thenReturn("1");
        when(reviewService.findUserReview(USER_ID, 7)).thenReturn(null);

        servlet.doGet(request, response);

        verify(session).setAttribute("msg", "You have no review of this product to change.");
        verify(response).sendRedirect("product");
        verify(request, never()).getRequestDispatcher("pages/product-review.jsp");
    }
}
