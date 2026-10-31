using System.Net;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.AspNetCore.Mvc.ViewFeatures;
using PeluqueriaWeb.Services;

namespace PeluqueriaWeb.Filters;

/// <summary>Convierte los errores de la API en un mensaje visible y regresa a la página anterior.</summary>
public class ApiExceptionFilter : IExceptionFilter
{
    private readonly ITempDataDictionaryFactory _tempData;
    public ApiExceptionFilter(ITempDataDictionaryFactory tempData) => _tempData = tempData;

    public void OnException(ExceptionContext ctx)
    {
        if (ctx.Exception is not ApiException ex) return;
        _tempData.GetTempData(ctx.HttpContext)["Error"] = ex.Message;
        var anterior = ctx.HttpContext.Request.Headers.Referer.ToString();
        ctx.Result = ex.Status == HttpStatusCode.Unauthorized
            ? new RedirectToActionResult("Salir", "Cuenta", null)
            : new RedirectResult(string.IsNullOrEmpty(anterior) ? "/" : anterior);
        ctx.ExceptionHandled = true;
    }
}
