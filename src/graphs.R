library(ggplot2)
library(plotly)

pl <- function(title,da,outfile=NULL)
{
    if (!is.null(outfile))    {png(paste(outfile,".png",sep=''),width=600,height=360)}
    p <- ggplot(data=subset(da,time>max(d$time)-200), aes(x=time, y=total, col=class)) + geom_line() + geom_point(size=1) + xlab("time of tournament [hours]") + ggtitle(paste(title, "- last 9 days"))
    print(p)
    if (!is.null(outfile))  {dev.off()}
    sp <- ggplotly(p)
    htmlwidgets::saveWidget(as.widget(sp), paste(outfile,".html",sep=''),libdir="lib")
}

pls <- function(title,da,outfile=NULL)
{
    if (!is.null(outfile))    {png(paste(outfile,".smooth.png",sep=''),width=600,height=360)}
    p <- ggplot(data=da, aes(x=time, y=total, col=class, fill=class)) + geom_smooth() + xlab("time of tournament [hours]") + ggtitle(title)
    print(p)
    if (!is.null(outfile))  {dev.off()}
    sp <- ggplotly(p)
    htmlwidgets::saveWidget(as.widget(sp), paste(outfile,".smooth.html",sep=''),libdir="lib")
}


setwd("log")
d = read.table("log2.tsv",sep="\t",header=T,quote="")
d$total = as.numeric(as.character(d$total))  # in case of extraneous headers
d$time = as.numeric(as.character(d$time))

# the tournament code uses unix time in hours minus
# some value as a basis; this is not very informative
d$time = d$time-min(d$time)  # arbitrary start time

w <- d[grep(".Wolf$", d$class), ]
s <- d[grep(".Sheep$", d$class), ]
wt <- d[grep(".WolfTeam$", d$class), ]
st <- d[grep(".SheepTeam$", d$class), ]

ws <- rbind(data.frame(w, type="wolf"), data.frame(s, type="sheep"), data.frame(subset(s, class=="dodds.Sheep"),type="dodds.Sheep"), data.frame(subset(w, class=="gehr.Wolf"), type="gehr.Wolf"))
ws$lty <- (ws$type!="sheep"&ws$type!="wolf")

## pl("Wolves", w)

pl("Wolves", w, "w")
pl("Sheep", s,"s")
pl("Wolf Teams", wt, "wt")
pl("Sheep Teams", st, "st")
pls("Wolves", w, "w")
pls("Sheep", s,"s")
pls("Wolf Teams", wt, "wt")
pls("Sheep Teams", st, "st")


# smooth plot
# ggplot(data=subset(s, total>.3 & time>max(d$time)-200), aes(x=time, y=total, col=class, fill=class)) + geom_smooth() + xlab("time of tournament [hours]") + ggtitle("Sheep - last 9 days")
# ggplot(data=subset(w, total>.3 & time>max(d$time)-200), aes(x=time, y=total, col=class, fill=class)) + geom_smooth() + xlab("time of tournament [hours]") + ggtitle("Wolves - last 9 days")
# ggplot(data=subset(st, total>.3 & time>max(d$time)-200), aes(x=time, y=total, col=class, fill=class)) + geom_smooth() + xlab("time of tournament [hours]") + ggtitle("SheepTeams - last 9 days")
# ggplot(data=subset(wt, total>.3 & time>max(d$time)-200), aes(x=time, y=total, col=class, fill=class)) + geom_smooth() + xlab("time of tournament [hours]") + ggtitle("WolfTeams - last 9 days")

# aggregate
# ggplot(data=subset(ws,total>.2), aes(x=time, y=total, col=type, fill=type, linetype=lty)) + geom_smooth() + xlab("time of tournament [hours]") + ggtitle("Wolves vs. Sheep")