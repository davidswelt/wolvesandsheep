library(ggplot2)
library(plotly)

pl <- function(title,da,outfile=NULL)
{
    if (!is.null(outfile))    {png(paste(outfile,".png",sep=''),width=1000,height=600)}
    p <- ggplot(data=da, aes(x=time, y=total, col=class)) + geom_line() + geom_point(size=1) + xlab("time of tournament [hours]") + ggtitle(title)
    print(p)
    if (!is.null(outfile))  {dev.off()}
    sp <- ggplotly(p)
    htmlwidgets::saveWidget(as.widget(sp), paste(outfile,".html",sep=''),libdir="lib")
}


setwd("log")
d = read.table("log.tsv",sep="\t",header=T,quote="")
d$total = as.numeric(as.character(d$total))  # in case of extraneous headers
d$time = as.numeric(as.character(d$time))

# the tournament code uses unix time in hours minus
# some value as a basis; this is not very informative
d$time = d$time-min(d$time)  # arbitrary start time

w <- d[grep(".Wolf$", d$class), ]
s <- d[grep(".Sheep$", d$class), ]
wt <- d[grep(".WolfTeam$", d$class), ]
st <- d[grep(".SheepTeam$", d$class), ]

## pl("Wolves", w)

pl("Wolves", w, "w")
pl("Sheep", s,"s")
pl("Wolf Teams", wt, "wt")
pl("Sheep Teams", st, "st")


