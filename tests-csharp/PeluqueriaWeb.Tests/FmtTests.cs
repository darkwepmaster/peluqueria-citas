using PeluqueriaWeb.Models;
using Xunit;

namespace PeluqueriaWeb.Tests;

public class FmtTests
{
    [Fact] public void DineroUsaSolesConDosDecimales() => Assert.Equal("S/ 35.00", Fmt.Dinero(35m));
    [Fact] public void HoraSeMuestraEn24Horas() => Assert.Equal("15:00", Fmt.Hora(new TimeOnly(15, 0)));
    [Theory, InlineData(1, "Lunes"), InlineData(6, "Sábado"), InlineData(7, "Domingo")]
    public void DiaDeLaSemana(int numero, string esperado) => Assert.Equal(esperado, Fmt.Dia(numero));

    [Fact]
    public void LaCitaActivaEsPendienteOConfirmada()
    {
        var c = new CitaDto(1, 1, "A", 2, "B", new DateOnly(2026, 6, 4), new TimeOnly(15, 0), new TimeOnly(15, 40), "PENDIENTE", 35m, new() { "Corte" }, null);
        Assert.True(c.EsActiva);
        Assert.False((c with { Estado = "CANCELADA" }).EsActiva);
        Assert.Equal("Corte", c.Descripcion);
    }

    [Fact]
    public void ElResumenDeReservaCalculaLaHoraDeFin()
    {
        var vm = new ConfirmarVm { Servicio = new ServicioDto(1, "Corte", null, "Corte", 35m, 40, true), Hora = new TimeOnly(15, 0) };
        Assert.Equal(new TimeOnly(15, 40), vm.Fin);
    }
}
