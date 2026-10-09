package com.fivesense.api.auth.controller;
import com.fivesense.api.shared.error.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.*;
class RoleAccessInterceptorTests {
    private final RoleAccessInterceptor interceptor=new RoleAccessInterceptor();
    private boolean call(String method,String uri,String role){
        var request=new MockHttpServletRequest(method,uri);
        if(role!=null)request.addHeader(RoleAccessInterceptor.ROLE_HEADER,role);
        return interceptor.preHandle(request,new MockHttpServletResponse(),new Object());
    }
    private HttpStatus statusOf(String method,String uri,String role){
        try{call(method,uri,role);return HttpStatus.OK;}catch(ApiException ex){return ex.getStatus();}
    }
    @Test void missingOrInvalidRoleIsUnauthorized(){
        assertThat(statusOf("GET","/api/v1/teams",null)).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(statusOf("GET","/api/v1/teams","  ")).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(statusOf("GET","/api/v1/teams","BOSS")).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    @Test void viewerCanReadReportAdjustStockAndChangeTeamStatus(){
        assertThat(statusOf("GET","/api/v1/users","VIEWER")).isEqualTo(HttpStatus.OK);
        assertThat(statusOf("POST","/api/v1/occurrences","VIEWER")).isEqualTo(HttpStatus.OK);
        assertThat(statusOf("POST","/api/v1/occurrences/images","viewer")).isEqualTo(HttpStatus.OK);
        assertThat(statusOf("PATCH","/api/v1/materials/1/stock","VIEWER")).isEqualTo(HttpStatus.OK);
        assertThat(statusOf("PATCH","/api/v1/teams/1/status","VIEWER")).isEqualTo(HttpStatus.OK);
    }
    @Test void viewerCannotCreateEditOrDeleteManagedResources(){
        for(String resource:new String[]{"users","problems","materials","teams"}){
            assertThat(statusOf("POST","/api/v1/"+resource,"VIEWER")).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(statusOf("PUT","/api/v1/"+resource+"/1","VIEWER")).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(statusOf("DELETE","/api/v1/"+resource+"/1","VIEWER")).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }
    @Test void adminAndManagerCanManageResources(){
        for(String role:new String[]{"ADMIN","MANAGER"}){
            assertThat(statusOf("POST","/api/v1/users",role)).isEqualTo(HttpStatus.OK);
            assertThat(statusOf("DELETE","/api/v1/materials/1",role)).isEqualTo(HttpStatus.OK);
        }
    }
    @Test void preflightRequestsPass(){assertThat(statusOf("OPTIONS","/api/v1/users",null)).isEqualTo(HttpStatus.OK);}
}
